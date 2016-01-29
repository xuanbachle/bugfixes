package util.ast.visitor;

import org.eclipse.jdt.core.dom.*;

import java.util.Set;

public class NameCollector extends ASTVisitor
{
	private Set<String> nameSet;
	
	public NameCollector(Set<String> o)
	{
		nameSet = o;
	}
	
	@Override
	public boolean visit(SimpleName node)
	{
		String name = node.getIdentifier();
		/*Boolean to_add = true;
		IBinding binding = node.resolveBinding();

		if(node.getParent() instanceof FieldAccess || (node.getParent() instanceof QualifiedName) || node.getParent() instanceof VariableDeclarationStatement){
			to_add = false;
		}
		if(to_add && binding instanceof IVariableBinding)*/

		nameSet.add(name);
		return super.visit(node);
	}
}
